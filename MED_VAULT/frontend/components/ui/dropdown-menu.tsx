import * as React from 'react'
import { cn } from '@/lib/utils'

const DropdownMenu = ({ children }: { children: React.ReactNode }) => <div className='relative'>{children}</div>

const DropdownMenuTrigger = ({ children }: { children: React.ReactNode; className?: string; asChild?: boolean }) => <>{children}</>

const DropdownMenuContent = ({ className, children }: { className?: string; align?: 'start' | 'end'; children: React.ReactNode }) => (
  <div className={cn('absolute right-0 mt-2 rounded-md border border-slate-200 bg-white p-1 shadow-lg', className)}>{children}</div>
)

const DropdownMenuGroup = ({ children }: { children: React.ReactNode }) => <div>{children}</div>

const DropdownMenuItem = ({ className, children }: { className?: string; children: React.ReactNode }) => (
  <div className={cn('cursor-pointer rounded px-2 py-1.5 text-sm hover:bg-slate-100', className)}>{children}</div>
)

export { DropdownMenu, DropdownMenuTrigger, DropdownMenuContent, DropdownMenuGroup, DropdownMenuItem }
