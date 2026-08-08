/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Deep movie platform colors (Letterboxd / TMDB dark mode style)
        brand: {
          light: '#24c55e', // Emerald highlight
          DEFAULT: '#10b981', // Main Accent
          dark: '#047857',
        },
        bg: {
          deep: '#0b0f19',    // Ultra-dark background
          card: '#161d30',    // Dark card background
          nav: 'rgba(11, 15, 25, 0.75)', // Blurry glass navbar
          input: '#1f293d',  // Input container colors
        },
        text: {
          primary: '#f3f4f6',  // Off-white primary text
          secondary: '#9ca3af', // Gray secondary text
          muted: '#6b7280',     // Darker gray muted text
        }
      },
      fontFamily: {
        sans: ['Outfit', 'Inter', 'sans-serif'],
      },
      backdropBlur: {
        xs: '2px',
      },
      boxShadow: {
        'glass-light': '0 8px 32px 0 rgba(31, 38, 135, 0.07)',
        'glass-card': '0 8px 32px 0 rgba(0, 0, 0, 0.37)',
      },
      borderWidth: {
        'glass': '1px',
      },
      borderColor: {
        'glass-light': 'rgba(255, 255, 255, 0.08)',
      },
      animation: {
        'fade-in': 'fadeIn 0.3s ease-out forwards',
        'slide-up': 'slideUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards',
      },
      keyframes: {
        fadeIn: {
          '0%': { opacity: '0' },
          '100%': { opacity: '1' },
        },
        slideUp: {
          '0%': { transform: 'translateY(12px)', opacity: '0' },
          '100%': { transform: 'translateY(0)', opacity: '1' },
        }
      }
    },
  },
  plugins: [],
}
