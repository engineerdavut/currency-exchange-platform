
export const formatCurrency = (amount: number, currency: string): string => {
  const formatter = new Intl.NumberFormat('tr-TR', {
    style: 'currency',
    currency: getCurrencyCode(currency),
    minimumFractionDigits: 2,
    maximumFractionDigits: 4,
  });

  return formatter.format(amount);
};

const getCurrencyCode = (currency: string): string => {
  switch (currency.toUpperCase()) {
    case 'TL':
    case 'TRY':
      return 'TRY';
    case 'USD':
      return 'USD';
    case 'EUR':
      return 'EUR';
    case 'GOLD':
      return 'XAU';
    default:
      return 'TRY';
  }
};
