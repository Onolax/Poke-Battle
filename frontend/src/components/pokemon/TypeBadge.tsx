const TYPE_CLASSES: Record<string, string> = {
  Normal: 'bg-zinc-500 text-white',
  Fire: 'bg-orange-500 text-white',
  Water: 'bg-blue-500 text-white',
  Grass: 'bg-green-500 text-white',
  Electric: 'bg-yellow-400 text-black',
  Ice: 'bg-cyan-300 text-black',
  Fighting: 'bg-red-700 text-white',
  Poison: 'bg-purple-500 text-white',
  Ground: 'bg-amber-600 text-white',
  Flying: 'bg-indigo-400 text-white',
  Psychic: 'bg-pink-500 text-white',
  Bug: 'bg-lime-500 text-black',
  Rock: 'bg-yellow-700 text-white',
  Ghost: 'bg-violet-700 text-white',
  Dragon: 'bg-violet-500 text-white',
  Dark: 'bg-zinc-700 text-white',
  Steel: 'bg-slate-400 text-black',
  Fairy: 'bg-pink-300 text-black',
}

interface Props {
  type: string
  size?: 'sm' | 'md'
}

export function TypeBadge({ type, size = 'sm' }: Props) {
  const cls = TYPE_CLASSES[type] ?? 'bg-zinc-600 text-white'
  const padCls = size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-3 py-1 text-sm'
  return (
    <span className={`${cls} ${padCls} rounded font-medium uppercase tracking-wide`}>
      {type}
    </span>
  )
}
