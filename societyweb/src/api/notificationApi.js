import axiosClient from './axiosClient';
export const notificationApi = {
    list: async () => (await axiosClient.get('/notifications')).data,
    markRead: async (id) => { await axiosClient.patch(`/notifications/${id}/read`); },
    markAllRead: async () => { await axiosClient.post('/notifications/read-all'); },
};