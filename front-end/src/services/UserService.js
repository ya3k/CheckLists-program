import axios from "./customize-axios";

const addUser = async (userName, email) => {
    try {
        const response = await axios.post(`/users/add`, {
            userName: userName,
            email: email,
        });
        return response.data;
    } catch (error) {
        throw error.response.data;
    }
};

const UserService = {
    addUser,
};

export default UserService;
