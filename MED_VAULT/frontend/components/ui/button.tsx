import * as React from 'react'
import { cn } from '@/lib/utils'

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: 'default' | 'outline' | 'ghost'
  size?: 'default' | 'icon'
  asChild?: boolean
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = 'default', size = 'default', asChild, children, ...props }, ref) => {
    const base = 'inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors disabled:opacity-50'
    const variants = {
      default: 'bg-black text-white dark:bg-white dark:text-black px-4 py-2',
      outline: 'border border-slate-300 px-4 py-2',
      ghost: 'hover:bg-slate-100 px-2 py-2'
    }
    const sizes = {
      default: '',
      icon: 'h-9 w-9 p-0'
    }

    if (asChild && React.isValidElement(children)) {
      return React.cloneElement(children as React.ReactElement, {
        className: cn(base, variants[variant], sizes[size], className, (children as React.ReactElement).props.className)
      })
    }

    return (
      <button ref={ref} className={cn(base, variants[variant], sizes[size], className)} {...props}>
        {children}
      </button>
    )
  }
)

Button.displayName = 'Button'

export { Button }
