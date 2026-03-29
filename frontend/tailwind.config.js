/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        base: '#09090b',      // zinc-950 — page background
        surface: '#18181b',   // zinc-900 — cards, panels
        elevated: '#27272a',  // zinc-800 — modals, inputs, hover
        accent: {
          DEFAULT: '#ef4444', // red-500
          hover: '#dc2626',   // red-600
          dim: '#450a0a',     // red-950
        },
        hp: {
          high: '#22c55e',    // green-500
          mid: '#eab308',     // yellow-500
          low: '#ef4444',     // red-500
        },
      },
    },
  },
  plugins: [],
}
