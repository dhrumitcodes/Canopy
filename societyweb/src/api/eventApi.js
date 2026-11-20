import axiosClient from './axiosClient';
export const eventApi = {
    getSocietyEvents: async (societyId, params) => (await axiosClient.get(`/societies/${societyId}/events`, { params })).data,
    getEvents: async (params) => (await axiosClient.get('/events', { params })).data,
    getEventById: async (societyId, eventId) => (await axiosClient.get(`/societies/${societyId}/events/${eventId}`)).data,
    createEvent: async (societyId, body) => (await axiosClient.post(`/societies/${societyId}/events`, body)).data,
    updateEvent: async (societyId, eventId, body) => (await axiosClient.put(`/societies/${societyId}/events/${eventId}`, body)).data,
    deleteEvent: async (societyId, eventId) => { await axiosClient.delete(`/societies/${societyId}/events/${eventId}`); },
    registerForEvent: async (eventId) => (await axiosClient.post(`/events/${eventId}/register`)).data,
    cancelRegistration: async (eventId) => { await axiosClient.delete(`/events/${eventId}/register`); },
    getMyRegistrations: async () => (await axiosClient.get('/events/my-registrations')).data,
    getAttendees: async (eventId) => (await axiosClient.get(`/events/${eventId}/attendees`)).data,
    checkIn: async (qrToken) => (await axiosClient.post('/events/check-in', { qrToken })).data,
    getPendingApprovals: async () => (await axiosClient.get('/college/events/pending')).data,
    approveEvent: async (eventId) => (await axiosClient.post(`/college/events/${eventId}/approve`)).data,
    rejectEvent: async (eventId, reason) => (await axiosClient.post(`/college/events/${eventId}/reject`, { reason })).data,
};