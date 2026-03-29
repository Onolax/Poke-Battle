import { Component, type ReactNode } from 'react'

interface Props { children: ReactNode }
interface State { error: Error | null }

export class ErrorBoundary extends Component<Props, State> {
  state: State = { error: null }

  static getDerivedStateFromError(error: Error): State {
    return { error }
  }

  render() {
    if (this.state.error) {
      return (
        <div className="min-h-screen flex items-center justify-center px-4">
          <div className="max-w-md text-center space-y-4">
            <p className="text-4xl">💥</p>
            <p className="text-accent font-bold text-lg">Something crashed</p>
            <pre className="text-zinc-500 text-xs bg-elevated rounded p-4 text-left overflow-auto max-h-48">
              {this.state.error.message}
            </pre>
            <button
              onClick={() => window.location.reload()}
              className="bg-accent hover:bg-accent-hover text-white text-sm px-4 py-2 rounded"
            >
              Reload
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}
