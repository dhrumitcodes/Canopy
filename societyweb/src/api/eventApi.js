import axiosClient from './axiosClient';
export const eventApi = {
 getSocietyEvents: async (id,params)=>(await axiosClient.get(`/societies/${id}/events`,{params})).data,
 getEvents: async (params)=>(await axiosClient.get('/events',{params})).data,
 getEventById: async (s,e)=>(await axiosClient.get(`/societies/${s}/events/${e}`)).data,
 createEvent: async (s,b)=>(await axiosClient.post(`/societies/${s}/events`,b)).data,
 updateEvent: async (s,e,b)=>(await axiosClient.put(`/societies/${s}/events/${e}`,b)).data,
 deleteEvent: async (s,e)=>{await axiosClient.delete(`/societies/${s}/events/${e}`);},
 registerForEvent: async (e)=>(await axiosClient.post(`/events/${e}/register`)).data,
 cancelRegistration: async (e)=>{await axiosClient.delete(`/events/${e}/register`);},
 getMyRegistrations: async ()=>(await axiosClient.get('/events/my-registrations')).data,
 getAttendees: async (e)=>(await axiosClient.get(`/events/${e}/attendees`)).data,
 checkIn: async (token)=>(await axiosClient.post('/events/check-in',{qrToken:token})).data,
 getPendingApprovals: async ()=>(await axiosClient.get('/college/events/pending')).data,
 approveEvent: async (e)=>(await axiosClient.post(`/college/events/${e}/approve`)).data,
 rejectEvent: async (e,r)=>(await axiosClient.post(`/college/events/${e}/reject`,{reason:r})).data,
 submitFeedback: async (e,b)=>(await axiosClient.post(`/events/${e}/feedback`,b)).data,
 getFeedback: async (e)=>(await axiosClient.get(`/events/${e}/feedback`)).data,
 downloadCertificate: async (e)=>(await axiosClient.get(`/events/${e}/certificate`,{responseType:'blob'})).data
};
