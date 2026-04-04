import { create } from 'zustand';

export interface User {
  id: number;
  nickname: string;
  email: string;
  profileImage: string;
}

interface AuthState {
  user: User | null;
  setUser: (user: User) => void;
  clearUser: () => void;
}

const useAuthStore = create<AuthState>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
  clearUser: () => set({ user: null }),
}));

export default useAuthStore;
