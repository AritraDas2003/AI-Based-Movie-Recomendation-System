import React from 'react'

export default function App() {
  return (
    <div className="min-h-screen flex flex-col justify-center items-center px-4 bg-[#0b0f19] text-[#f3f4f6]">
      <div className="glass-panel max-w-lg w-full text-center p-8 rounded-2xl animate-slide-up">
        <div className="text-6xl mb-4">🎬</div>
        <h1 className="text-4xl font-extrabold mb-2 tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-emerald-400 to-emerald-600">
          CineMatch
        </h1>
        <p className="text-[#9ca3af] text-sm mb-6 font-sans">
          Conversational AI Movie Recommendation Platform
        </p>
        <div className="border border-white/5 p-4 rounded-xl mb-6 bg-[#161d30]/50 text-left text-xs space-y-2 font-mono">
          <p className="font-semibold text-emerald-400 text-sm font-sans">Frontend Scaffold Ready!</p>
          <p>• Vite dev server configurations active</p>
          <p>• Tailwind CSS & Outfit typography active</p>
          <p>• PostCSS & proxy mappings initialized</p>
        </div>
        <p className="text-[#6b7280] text-xs">
          MCA Project • Git Monorepo Structure
        </p>
      </div>
    </div>
  )
}
