import { useMutation } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import { api, decodeJwtPayload } from '@/lib/axios'
import { useAuthStore } from '@/stores/authStore'

interface RegisterBody {
  username: string
  email: string
  password: string
}

interface LoginBody {
  username: string
  password: string
}

function extractErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data
    if (typeof data === 'string') return data
    if (data?.message) return data.message
    if (data?.error) return data.error
    if (error.response?.status === 401) return 'Invalid username or password.'
    if (error.response?.status === 400) return 'Invalid request. Check your input.'
  }
  return 'Something went wrong. Please try again.'
}

export function useLoginMutation() {
  const { login } = useAuthStore()
  const navigate = useNavigate()

  return useMutation({
    mutationFn: async (body: LoginBody) => {
      const { data } = await api.post<{ token: string }>('/auth/login', body)
      return data
    },
    onSuccess: ({ token }) => {
      const { sub, username } = decodeJwtPayload(token)
      login(token, sub, username)
      navigate('/pokemon')
    },
  })
}

export function useRegisterMutation() {
  const { login } = useAuthStore()
  const navigate = useNavigate()

  return useMutation({
    mutationFn: async (body: RegisterBody) => {
      // Register returns { userId, username } — no token
      await api.post('/auth/register', body)
      // Immediately log in to obtain the JWT
      const { data } = await api.post<{ token: string }>('/auth/login', {
        username: body.username,
        password: body.password,
      })
      return data
    },
    onSuccess: ({ token }) => {
      const { sub, username } = decodeJwtPayload(token)
      login(token, sub, username)
      navigate('/pokemon')
    },
  })
}

export { extractErrorMessage }
