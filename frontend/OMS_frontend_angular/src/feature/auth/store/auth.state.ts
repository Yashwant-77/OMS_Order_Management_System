
export const initialState: AuthState = {
  user: null,
  token: null,
  isLoading: false
};


export interface AuthState {
  user: {
    id: number;
    email: string;
    role: 'ADMIN' | 'SALES' | 'FINANCE' | 'PRODUCTION';
  } | null;

  token: string | null;
  isLoading: boolean;
}