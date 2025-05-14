import React from "react";

type ButtonVariant =
  | "primary"
  | "secondary"
  | "success"
  | "danger"
  | "warning"
  | "info";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: ButtonVariant;
  isLoading?: boolean;
  icon?: React.ReactNode;
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = "primary",
  isLoading = false,
  icon,
  children,
  className = "",
  disabled,
  ...props
}) => {
  const baseClasses =
    "inline-flex items-center justify-center px-4 py-2 border rounded-md font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors";

  const variantClasses = {
    primary:
      "bg-blue-600 hover:bg-blue-700 text-white border-transparent focus:ring-blue-500",
    secondary:
      "bg-gray-200 hover:bg-gray-300 text-gray-800 border-transparent focus:ring-gray-500",
    success:
      "bg-green-600 hover:bg-green-700 text-white border-transparent focus:ring-green-500",
    danger:
      "bg-red-600 hover:bg-red-700 text-white border-transparent focus:ring-red-500",
    warning:
      "bg-yellow-500 hover:bg-yellow-600 text-white border-transparent focus:ring-yellow-500",
    info: "bg-indigo-600 hover:bg-indigo-700 text-white border-transparent focus:ring-indigo-500",
  };

  const disabledClasses = "opacity-50 cursor-not-allowed";

  const buttonClasses = `
    ${baseClasses} 
    ${variantClasses[variant]} 
    ${disabled || isLoading ? disabledClasses : ""}
    ${className}
  `;

  return (
    <button
      className={buttonClasses}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? (
        <div className="flex items-center">
          <svg
            className="animate-spin -ml-1 mr-2 h-4 w-4 text-current"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            ></circle>
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
          <span>Loading...</span>
        </div>
      ) : (
        <div className="flex items-center">
          {icon && <span className="mr-2">{icon}</span>}
          {children}
        </div>
      )}
    </button>
  );
};
