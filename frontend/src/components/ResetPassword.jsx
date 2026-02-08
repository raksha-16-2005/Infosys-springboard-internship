import React, {useState} from 'react'
import axios from 'axios'
import { useSearchParams, useNavigate } from 'react-router-dom'

export default function ResetPassword(){
  const [search]=useSearchParams()
  const token = search.get('token')
  const [pw,setPw]=useState('')
  const [pw2,setPw2]=useState('')
  const [msg,setMsg]=useState('')
  const [loading, setLoading] = useState(false)
  const nav = useNavigate()
  
  const submit=async e=>{
    e.preventDefault()
    if(!pw || !pw2){setMsg('Please fill all fields'); return}
    if(pw!==pw2){setMsg('Passwords do not match'); return}
    if(pw.length < 6){setMsg('Password must be at least 6 characters'); return}
    
    setLoading(true)
    try{
      await axios.post('/api/auth/reset-password',{token, newPassword:pw})
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      setMsg('Password reset successful! Redirecting to login...')
      setTimeout(() => nav('/'), 2000)
    }catch(err){ 
      setMsg(err?.response?.data || 'Error resetting password') 
    }finally{
      setLoading(false)
    }
  }
  
  return (
    <div style={{maxWidth: 400, margin: '50px auto', padding: 20}}>
      <h2>Reset Password</h2>
      {msg && (
        <div style={{padding: '10px', marginBottom: '15px', backgroundColor: msg.includes('successful') ? '#efe' : '#fee', border: `1px solid ${msg.includes('successful') ? '#9f9' : '#f99'}`, borderRadius: '4px', color: msg.includes('successful') ? '#3c3' : '#c33'}}>
          {msg}
        </div>
      )}
      <form onSubmit={submit}>
        <div style={{marginBottom: 15}}>
          <label>New Password</label>
          <input type="password" value={pw} onChange={e=>setPw(e.target.value)} placeholder="Enter new password" style={{width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ddd'}} />
        </div>
        <div style={{marginBottom: 15}}>
          <label>Confirm Password</label>
          <input type="password" value={pw2} onChange={e=>setPw2(e.target.value)} placeholder="Confirm password" style={{width: '100%', padding: '8px', borderRadius: '4px', border: '1px solid #ddd'}} />
        </div>
        <button type="submit" disabled={loading} className="btn btn-primary" style={{width: '100%'}}>
          {loading ? 'Resetting...' : 'Reset Password'}
        </button>
      </form>
    </div>
  )
}
