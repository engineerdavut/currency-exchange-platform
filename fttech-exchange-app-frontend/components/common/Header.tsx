// src/components/common/Header.tsx (Props ile Güncellenmiş)
// Bu bileşen artık client olmak zorunda değil, props alıyorsa Server Component olabilir.
// Ancak mobil menü state'i varsa client kalır. Şimdilik client bırakalım.


import React, { useState } from 'react';
import Link from 'next/link';
// import { useRouter } from 'next/navigation'; // Logout sonrası yönlendirme Layout'ta veya Slice'ta yapılabilir

interface HeaderProps {
  isAuthenticated: boolean;
  username?: string; // Opsiyonel kullanıcı adı
  onLogout: () => void; // Logout fonksiyonu prop'u
}

export const Header: React.FC<HeaderProps> = ({ isAuthenticated, username, onLogout }) => {
  // const router = useRouter(); // Eğer logout sonrası yönlendirme burada yapılacaksa
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const handleLogoutClick = () => {
    onLogout(); // Layout'tan gelen logout fonksiyonunu çağır
    // router.push('/login'); // Yönlendirme burada veya Layout'ta yapılabilir
    setIsMobileMenuOpen(false); // Mobil menüyü kapat
  };

  return (
    <header className="p-4 shadow bg-white">
      <div className="max-w-7xl mx-auto flex justify-between items-center">
        <Link href="/" className="text-xl font-bold text-blue-600" onClick={() => setIsMobileMenuOpen(false)}>
          Exchange Platform
        </Link>

        {/* Mobile menu button */}
        <button
          className="md:hidden p-2 rounded-md text-gray-500 hover:text-gray-700 focus:outline-none"
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          aria-label="Toggle menu"
        >
          {/* SVG Icon */}
           <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
             <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={isMobileMenuOpen ? "M6 18L18 6M6 6l12 12" : "M4 6h16M4 12h16M4 18h16"} />
           </svg>
        </button>

        {/* Desktop navigation */}
        <nav className="hidden md:flex space-x-6 items-center">
          {!isAuthenticated ? (
            <>
              <Link href="/login" className="text-gray-700 hover:text-blue-600 transition-colors">Login</Link>
              <Link href="/register" className="text-gray-700 hover:text-blue-600 transition-colors">Register</Link>
            </>
          ) : (
            <>
              <span className="text-sm text-gray-600 mr-2">Welcome, {username || 'User'}!</span>
              <Link href="/account" className="text-gray-700 hover:text-blue-600 transition-colors">Wallet</Link>
              <Link href="/exchange" className="text-gray-700 hover:text-blue-600 transition-colors">Exchange</Link>
              <Link href="/transactions" className="text-gray-700 hover:text-blue-600 transition-colors">Transactions</Link>
              <button onClick={handleLogoutClick} className="text-gray-700 hover:text-blue-600 transition-colors">
                Logout
              </button>
            </>
          )}
        </nav>
      </div>

      {/* Mobile navigation */}
      {isMobileMenuOpen && (
        <div className="md:hidden mt-2 pt-2 border-t">
          <nav className="flex flex-col space-y-3">
            {!isAuthenticated ? (
               <>
                 <Link href="/login" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50" onClick={() => setIsMobileMenuOpen(false)}>Login</Link>
                 <Link href="/register" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50" onClick={() => setIsMobileMenuOpen(false)}>Register</Link>
               </>
             ) : (
               <>
                 <span className="block px-3 py-2 text-base font-medium text-gray-500">Welcome, {username || 'User'}!</span>
                 <Link href="/account" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50" onClick={() => setIsMobileMenuOpen(false)}>Wallet</Link>
                 <Link href="/exchange" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50" onClick={() => setIsMobileMenuOpen(false)}>Exchange</Link>
                 <Link href="/transactions" className="block px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50" onClick={() => setIsMobileMenuOpen(false)}>Transactions</Link>
                 <button onClick={handleLogoutClick} className="block w-full text-left px-3 py-2 rounded-md text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50">
                   Logout
                 </button>
               </>
             )}
          </nav>
        </div>
      )}
    </header>
  );
};
