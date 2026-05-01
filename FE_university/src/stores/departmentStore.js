// 04_university/src/stores/departmentStore.js
import { defineStore } from "pinia";
import apiClient from '@/api';
import { reactive, ref } from "vue";

export const useDepartmentStore = defineStore('department', () => {

    const departments = ref([]);

    const fetchDepartments = async (page, numOfRows) => {
            const response = 
                await apiClient.get(`/api/v1/department-service/departments?page=${page}&numOfRows=${numOfRows}`);

            departments.value = response.data.item ?? response.data.items;


            return response.data;

    };
    const fetchDepartment = async (departmentNo) => {
        const response =
            await apiClient.get(`/api/v1/department-service/departments/${departmentNo}`);
            
        departments.value = response.data.item;
        
        return response.data;
    }

    const addDepartment = async(department) => {
        const response = await apiClient.post(
            '/api/v1/department-service/departments',
            department
        );

        return response.data;
        
    };

    const editDepartment = async (departmentNo,department) => {
        const response = 
            await apiClient.put(`/api/v1/department-service/departments/${departmentNo}`, department);

        return response.data;

    };

    const deleteDepartment = async(departmentNo) => {
        const response = 
            await apiClient.delete(`/api/v1/department-service/departments/${departmentNo}`);


        return response.data;


    }

    const clearState = () => {
        departments.value = [];
    };

    return {departments, fetchDepartments, fetchDepartment, addDepartment, editDepartment, deleteDepartment, clearState};

});
