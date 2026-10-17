import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, catchError, of, switchMap, tap, throwError } from 'rxjs';

export type TranslationDirection = 'TEXT_TO_MORSE' | 'MORSE_TO_TEXT';
export type TranslationSource = 'TEXT' | 'FILE';

export interface UserProfile {
  username: string;
}

export interface TranslationEntry {
  id: number;
  direction: TranslationDirection;
  source: TranslationSource;
  inputText: string;
  outputText: string;
  createdAt: string;
}

export interface TranslationPayload {
  direction: TranslationDirection;
  source: TranslationSource;
  inputText: string;
}

@Injectable({ providedIn: 'root' })
export class MorseApiService {
  private readonly http = inject(HttpClient);

  restoreSession(): Observable<UserProfile | null> {
    return this.csrf().pipe(
      switchMap(() => this.http.get<UserProfile>('/api/auth/me')),
      catchError((error: HttpErrorResponse) => error.status === 401
        ? of(null)
        : throwError(() => error))
    );
  }

  login(username: string, password: string): Observable<UserProfile> {
    return this.csrf().pipe(
      switchMap(() => this.http.post<UserProfile>('/api/auth/login', { username, password }))
    );
  }

  register(username: string, password: string): Observable<UserProfile> {
    return this.csrf().pipe(
      switchMap(() => this.http.post<UserProfile>('/api/auth/register', { username, password }))
    );
  }

  logout(): Observable<void> {
    return this.csrf().pipe(switchMap(() => this.http.post<void>('/api/auth/logout', {})));
  }

  translate(payload: TranslationPayload): Observable<TranslationEntry> {
    return this.http.post<TranslationEntry>('/api/translations', payload);
  }

  history(): Observable<TranslationEntry[]> {
    return this.http.get<TranslationEntry[]>('/api/history');
  }

  deleteHistoryEntry(id: number): Observable<void> {
    return this.http.delete<void>(`/api/history/${id}`);
  }

  clearHistory(): Observable<void> {
    return this.http.delete<void>('/api/history');
  }

  private csrf(): Observable<unknown> {
    return this.http.get('/api/auth/csrf');
  }
}
