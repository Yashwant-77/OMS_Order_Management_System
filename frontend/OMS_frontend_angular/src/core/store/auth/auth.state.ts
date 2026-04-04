


export interface AuthState {
  user: {
    id: number;
    email: string;
    role: 'ADMINISTRATOR' | 'PRODUCT_MANAGER' | 'SALES_REPRESENTATIVE' | 'PURCHASING_OFFICER' | 'FINANCE_MANAGER' | 'BUSINESS_ANALYST';
  } | null;

  token: string | null;
  isLoading: boolean;
}


export const initialState: AuthState = {
  user: null,
  token: null,
  isLoading: false
};
