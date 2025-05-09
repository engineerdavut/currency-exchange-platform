// app/page.tsx
import React from 'react';
import { Card } from '../components/common/Card';

export default function HomePage() {
  return (
    <div className="max-w-5xl mx-auto">
      <div className="text-center mb-12">
        <h1 className="text-4xl md:text-5xl font-bold mb-4 text-blue-600">
          Welcome to Exchange Platform
        </h1>
        <p className="text-xl text-gray-700 mb-8 max-w-3xl mx-auto">
          Enjoy real-time trading with TRY, USD, EUR, and Gold. Start with our secure and user-friendly platform today!
        </p>
        <div className="flex justify-center space-x-4">
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-12">
        <Card>
          <h3 className="text-xl font-semibold text-gray-800 mb-3">Real-Time Prices</h3>
          <p className="text-gray-600">Get the latest rates for all currencies with our real-time price tracking system.</p>
        </Card>
        <Card>
          <h3 className="text-xl font-semibold text-gray-800 mb-3">Secure Transactions</h3>
          <p className="text-gray-600">Your data and transactions are protected with top-level security protocols.</p>
        </Card>
        <Card>
          <h3 className="text-xl font-semibold text-gray-800 mb-3">User-Friendly Interface</h3>
          <p className="text-gray-600">Trade easily with our modern and intuitive design, built for both beginners and experts.</p>
        </Card>
        <Card>
          <h3 className="text-xl font-semibold text-gray-800 mb-3">24/7 Support</h3>
          <p className="text-gray-600">Our dedicated team is always available to assist you with any questions or issues.</p>
        </Card>
      </div>
    </div>
  );
}
