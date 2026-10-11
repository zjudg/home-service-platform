import { storeToRefs } from 'pinia'
import { useAuthStore } from '@/stores/auth'

export function useAuth () {
  const store = useAuthStore()
  const { token, user, loading, isLoggedIn } = storeToRefs(store)
  return { store, token, user, loading, isLoggedIn }
}
