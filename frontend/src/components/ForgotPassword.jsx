import React, {useState} from 'react'
import axios from 'axios'

export default function ForgotPassword(){
  const [email,setEmail]=useState('')
  const [msg,setMsg]=useState('')
  const submit=async e=>{
    e.preventDefault()
    try{
      await axios.post('/api/auth/forgot-password',{email})
      setMsg('If an account exists, a reset link was sent')
    }catch(err){ setMsg(err?.response?.data||'Error') }
  }
  return (
    <div style={{padding:20}}>
      <h3>Forgot Password</h3>
      <form onSubmit={submit}>
        <input value={email} onChange={e=>setEmail(e.target.value)} placeholder="Email" />
        <button type="submit">Send reset link</button>
      </form>
      <div>{msg}</div>
    </div>
  )
}
