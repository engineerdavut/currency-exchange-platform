import React from "react";
import Link from "next/link";

export const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="p-6 bg-white border-t mt-auto">
      <div className="max-w-7xl mx-auto">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <div className="mb-4 md:mb-0">
            <p className="text-gray-600">
              © {currentYear} Trading Platform. All rights reserved.
            </p>
          </div>

          <div className="flex space-x-6">
            <Link
              href="/about"
              className="text-gray-500 hover:text-gray-700 transition-colors"
            >
              About
            </Link>
            <Link
              href="/privacy"
              className="text-gray-500 hover:text-gray-700 transition-colors"
            >
              Privacy Policy
            </Link>
            <Link
              href="/terms"
              className="text-gray-500 hover:text-gray-700 transition-colors"
            >
              Terms of Service
            </Link>
            <Link
              href="/contact"
              className="text-gray-500 hover:text-gray-700 transition-colors"
            >
              Contact
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
};
