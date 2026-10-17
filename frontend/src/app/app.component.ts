import { CommonModule, DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  MorseApiService,
  TranslationDirection,
  TranslationEntry,
  UserProfile
} from './morse-api.service';

@Component({
  selector: 'morse-root',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  private readonly api = inject(MorseApiService);

  user: UserProfile | null = null;
  username = '';
  password = '';
  authMode: 'login' | 'register' = 'login';
  authBusy = false;
  authMessage = '';
  authError = false;
  connecting = true;

  direction: TranslationDirection = 'TEXT_TO_MORSE';
  inputText = '';
  outputText = '';
  translating = false;
  translationMessage = '';
  translationError = false;
  copyLabel = 'Copy';

  history: TranslationEntry[] = [];
  historyLoading = false;
  historyError = '';
  showHistoryOnMobile = false;
  showLearning = false;

  selectedFile: File | null = null;
  fileMessage = 'Text files up to 50 KB';
  fileError = false;
  fileTranslating = false;
  fileResult: TranslationEntry | null = null;

  readonly alphabet = [
    ['A', '.-'], ['B', '-...'], ['C', '-.-.'], ['D', '-..'], ['E', '.'], ['F', '..-.'], ['G', '--.'],
    ['H', '....'], ['I', '..'], ['J', '.---'], ['K', '-.-'], ['L', '.-..'], ['M', '--'], ['N', '-.'],
    ['O', '---'], ['P', '.--.'], ['Q', '--.-'], ['R', '.-.'], ['S', '...'], ['T', '-'], ['U', '..-'],
    ['V', '...-'], ['W', '.--'], ['X', '-..-'], ['Y', '-.--'], ['Z', '--..']
  ];

  ngOnInit(): void {
    this.api.restoreSession().subscribe({
      next: (user) => {
        this.user = user;
        this.connecting = false;
        if (user) this.loadHistory();
      },
      error: () => {
        this.connecting = false;
        this.authError = true;
        this.authMessage = 'Could not reach the Spring Boot server. Start it, then refresh this page.';
      }
    });
  }

  submitAuth(): void {
    this.authBusy = true;
    this.authMessage = '';
    this.authError = false;
    const request = this.authMode === 'register'
      ? this.api.register(this.username.trim(), this.password)
      : this.api.login(this.username.trim(), this.password);
    request.subscribe({
      next: (user) => {
        this.user = user;
        this.password = '';
        this.authBusy = false;
        this.authMessage = '';
        this.loadHistory();
      },
      error: (error: unknown) => {
        this.authBusy = false;
        this.authError = true;
        this.authMessage = this.messageFrom(error, this.authMode === 'register'
          ? 'Could not create the account.'
          : 'Could not sign in. Check your username and password.');
      }
    });
  }

  setAuthMode(mode: 'login' | 'register'): void {
    this.authMode = mode;
    this.authMessage = '';
    this.authError = false;
  }

  signOut(): void {
    this.api.logout().subscribe({
      next: () => this.resetSignedInState(),
      error: (error: unknown) => {
        if ((error as { status?: number }).status === 401) this.resetSignedInState();
        else {
          this.authError = true;
          this.authMessage = this.messageFrom(error, 'Could not sign out. Try again.');
        }
      }
    });
  }

  setDirection(direction: TranslationDirection): void {
    this.direction = direction;
    this.outputText = '';
    this.fileResult = null;
    this.translationMessage = '';
    this.fileMessage = this.selectedFile ? 'Direction changed. Translate this file again.' : 'Text files up to 50 KB';
  }

  translateMessage(): void {
    this.translationMessage = '';
    this.translationError = false;
    if (!this.inputText.trim()) {
      this.translationError = true;
      this.translationMessage = 'Enter a message to translate.';
      return;
    }
    this.translating = true;
    this.api.translate({ direction: this.direction, source: 'TEXT', inputText: this.inputText }).subscribe({
      next: (entry) => {
        this.outputText = entry.outputText;
        this.translating = false;
        this.translationMessage = 'Translation saved to your history.';
        this.addHistory(entry);
      },
      error: (error: unknown) => {
        this.translating = false;
        this.translationError = true;
        this.translationMessage = this.messageFrom(error, 'Translation failed. Try again.');
      }
    });
  }

  clearMessage(): void {
    this.inputText = '';
    this.outputText = '';
    this.translationMessage = '';
  }

  loadSample(): void {
    this.inputText = this.direction === 'TEXT_TO_MORSE'
      ? 'Hello, world! 123'
      : '.... . .-.. .-.. --- / .-- --- .-. .-.. -..';
    this.translationMessage = 'Sample loaded.';
    this.translationError = false;
  }

  async copyOutput(): Promise<void> {
    if (!this.outputText) return;
    try {
      await navigator.clipboard.writeText(this.outputText);
      this.copyLabel = 'Copied';
      window.setTimeout(() => this.copyLabel = 'Copy', 1300);
    } catch {
      this.translationError = true;
      this.translationMessage = 'Clipboard access is unavailable in this browser.';
    }
  }

  selectFile(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    input.value = '';
    this.fileResult = null;
    this.fileError = false;
    if (!file) return;
    if (!/\.txt$/i.test(file.name) && file.type !== 'text/plain') {
      this.rejectFile('Choose a plain .txt file.');
      return;
    }
    if (file.size > 50 * 1024) {
      this.rejectFile('File is larger than the 50 KB limit.');
      return;
    }
    this.selectedFile = file;
    this.fileMessage = `${file.name} · ready to translate`;
  }

  translateFile(): void {
    if (!this.selectedFile) return;
    this.fileTranslating = true;
    this.fileError = false;
    this.selectedFile.text().then((text) => {
      this.api.translate({ direction: this.direction, source: 'FILE', inputText: text }).subscribe({
        next: (entry) => {
          this.fileResult = entry;
          this.outputText = entry.outputText;
          this.inputText = entry.inputText;
          this.fileTranslating = false;
          this.fileMessage = 'File translated and saved to your history.';
          this.addHistory(entry);
        },
        error: (error: unknown) => {
          this.fileTranslating = false;
          this.fileError = true;
          this.fileMessage = this.messageFrom(error, 'File translation failed.');
        }
      });
    }).catch(() => {
      this.fileTranslating = false;
      this.fileError = true;
      this.fileMessage = 'This file could not be read as text.';
    });
  }

  downloadFile(): void {
    if (!this.fileResult) return;
    const fileStem = this.selectedFile?.name.replace(/\.[^.]+$/, '') || `morse-history-${this.fileResult.id}`;
    const suffix = this.direction === 'TEXT_TO_MORSE' ? '.morse.txt' : '.decoded.txt';
    const url = URL.createObjectURL(new Blob([this.fileResult.outputText], { type: 'text/plain;charset=utf-8' }));
    const link = document.createElement('a');
    link.href = url;
    link.download = `${fileStem}${suffix}`;
    document.body.append(link);
    link.click();
    link.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  loadHistory(): void {
    this.historyLoading = true;
    this.historyError = '';
    this.api.history().subscribe({
      next: (entries) => {
        this.history = entries;
        this.historyLoading = false;
      },
      error: (error: unknown) => {
        this.historyLoading = false;
        this.historyError = this.messageFrom(error, 'Could not load your history.');
      }
    });
  }

  toggleGuide(): void {
    this.showLearning = !this.showLearning;
    if (this.showLearning) {
      window.setTimeout(() => document.getElementById('learning-title')?.scrollIntoView({ behavior: 'smooth', block: 'start' }));
    }
  }

  restoreEntry(entry: TranslationEntry): void {
    this.direction = entry.direction;
    this.inputText = entry.inputText;
    this.outputText = entry.outputText;
    this.translationMessage = 'Loaded from your history.';
    this.translationError = false;
    this.showHistoryOnMobile = false;
    this.fileResult = entry.source === 'FILE' ? entry : null;
  }

  removeEntry(entry: TranslationEntry, event: Event): void {
    event.stopPropagation();
    this.api.deleteHistoryEntry(entry.id).subscribe({
      next: () => this.history = this.history.filter((item) => item.id !== entry.id),
      error: (error: unknown) => this.historyError = this.messageFrom(error, 'Could not delete that item.')
    });
  }

  clearHistory(): void {
    if (!this.history.length || !window.confirm('Clear your translation history? This cannot be undone.')) return;
    this.api.clearHistory().subscribe({
      next: () => {
        this.history = [];
        this.historyError = '';
      },
      error: (error: unknown) => this.historyError = this.messageFrom(error, 'Could not clear history.')
    });
  }

  directionLabel(direction: TranslationDirection): string {
    return direction === 'TEXT_TO_MORSE' ? 'Text to Morse' : 'Morse to text';
  }

  private addHistory(entry: TranslationEntry): void {
    this.history = [entry, ...this.history.filter((item) => item.id !== entry.id)].slice(0, 50);
    this.historyError = '';
  }

  private rejectFile(message: string): void {
    this.selectedFile = null;
    this.fileError = true;
    this.fileMessage = message;
  }

  private resetSignedInState(): void {
    this.user = null;
    this.history = [];
    this.inputText = '';
    this.outputText = '';
    this.selectedFile = null;
    this.fileResult = null;
    this.authMessage = '';
    this.showHistoryOnMobile = false;
  }

  private messageFrom(error: unknown, fallback: string): string {
    const body = (error as { error?: { message?: string } })?.error;
    return body?.message || fallback;
  }
}
