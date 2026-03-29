import { useEffect, useRef } from 'react'

interface Props {
  messages: string[]
}

export function BattleLog({ messages }: Props) {
  const bottomRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  return (
    <div className="bg-surface border border-zinc-800 rounded-lg p-4 h-full flex flex-col">
      <p className="text-xs text-zinc-600 uppercase tracking-wide mb-2">Battle Log</p>
      <div className="flex-1 overflow-y-auto space-y-1 text-sm pr-1">
        {messages.length === 0 ? (
          <p className="text-zinc-700 italic">Waiting for battle to begin…</p>
        ) : (
          messages.map((msg, i) => (
            <p key={i} className={`text-zinc-300 leading-snug ${
              msg.includes('super effective') ? 'text-green-400' :
              msg.includes('not very effective') ? 'text-red-400' :
              msg.includes('immune') ? 'text-zinc-500' :
              msg.includes('won') || msg.includes('Victory') ? 'text-yellow-400 font-medium' :
              ''
            }`}>
              {msg}
            </p>
          ))
        )}
        <div ref={bottomRef} />
      </div>
    </div>
  )
}
