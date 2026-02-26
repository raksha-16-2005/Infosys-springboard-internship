import { Search, Filter, UserRound } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'

const patients = [
  {
    id: 'PAT-1001',
    name: 'Arjun Mehta',
    age: 34,
    gender: 'Male',
    doctor: 'Dr. N. Rao',
    nextAppointment: '2026-03-02 10:30 AM',
    status: 'Active'
  },
  {
    id: 'PAT-1002',
    name: 'Sana Iqbal',
    age: 28,
    gender: 'Female',
    doctor: 'Dr. A. Verma',
    nextAppointment: '2026-03-03 02:00 PM',
    status: 'Follow-up'
  },
  {
    id: 'PAT-1003',
    name: 'Rohan Das',
    age: 46,
    gender: 'Male',
    doctor: 'Dr. K. Singh',
    nextAppointment: '2026-03-04 11:15 AM',
    status: 'Critical'
  }
]

export function PatientList01() {
  return (
    <main className='min-h-screen bg-gradient-to-b from-slate-50 to-white p-6'>
      <section className='mx-auto flex w-full max-w-6xl flex-col gap-6'>
        <Card>
          <CardHeader className='space-y-2'>
            <CardTitle className='flex items-center gap-2 text-2xl'>
              <UserRound className='h-6 w-6' />
              Patient List
            </CardTitle>
            <CardDescription>
              View, search, and manage currently registered patients.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='flex flex-col gap-3 sm:flex-row'>
              <div className='relative w-full'>
                <Search className='pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400' />
                <Input className='pl-9' placeholder='Search by patient name, ID, or doctor...' />
              </div>
              <Button variant='outline' className='gap-2'>
                <Filter className='h-4 w-4' />
                Filter
              </Button>
            </div>

            <Separator />

            <div className='overflow-x-auto'>
              <table className='w-full min-w-[760px] border-collapse text-sm'>
                <thead>
                  <tr className='border-b border-slate-200 text-left'>
                    <th className='px-3 py-2 font-semibold text-slate-700'>Patient ID</th>
                    <th className='px-3 py-2 font-semibold text-slate-700'>Name</th>
                    <th className='px-3 py-2 font-semibold text-slate-700'>Age/Gender</th>
                    <th className='px-3 py-2 font-semibold text-slate-700'>Assigned Doctor</th>
                    <th className='px-3 py-2 font-semibold text-slate-700'>Next Appointment</th>
                    <th className='px-3 py-2 font-semibold text-slate-700'>Status</th>
                    <th className='px-3 py-2 font-semibold text-slate-700'>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {patients.map((patient) => (
                    <tr key={patient.id} className='border-b border-slate-100'>
                      <td className='px-3 py-3'>{patient.id}</td>
                      <td className='px-3 py-3 font-medium text-slate-800'>{patient.name}</td>
                      <td className='px-3 py-3'>
                        {patient.age} / {patient.gender}
                      </td>
                      <td className='px-3 py-3'>{patient.doctor}</td>
                      <td className='px-3 py-3'>{patient.nextAppointment}</td>
                      <td className='px-3 py-3'>
                        <span className='rounded-full bg-slate-100 px-2 py-1 text-xs font-medium text-slate-700'>
                          {patient.status}
                        </span>
                      </td>
                      <td className='px-3 py-3'>
                        <Button variant='ghost' className='h-8 px-2'>
                          View
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </CardContent>
        </Card>
      </section>
    </main>
  )
}
