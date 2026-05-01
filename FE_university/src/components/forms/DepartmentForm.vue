<template>
        <form @submit.prevent="submitClick">
        <div class="mb-3">
            <label for="name" class="form-label">학과 이름</label>
            <input type="text" class="form-control" id="name" v-model="formData.name" required>
        </div>
        <div class="mb-3">
            <label for="category" class="form-label">계열</label>
            <input type="text" class="form-control" id="category" v-model="formData.category" required>
        </div>
        <div class="mb-3">
            <label for="openYn" class="form-label">개설 여부</label>
            <select class="form-select" id="openYn" v-model="formData.openYn">
                <option value="Y">예(Y)</option>
                <option value="N">아니오(N)</option>
            </select>
        </div>
        <div class="mb-3">
            <label for="capacity" class="form-label">정원</label>
            <input type="number" class="form-control" v-model="formData.capacity">
        </div>

        <div v-if="formType === 'add'">
            <button type="submit" class="btn btn-success m-1">학과 등록</button>
            <button type="reset" class="btn btn-secondary m-1">초기화</button>
        </div>

        <div v-if="formType === 'edit'">
            <button type="submit" class="btn btn-primary m-1">학과 수정</button>
            <button type="button" class="btn btn-danger m-1">학과 삭제</button>
        </div>

    </form>
</template>

<script setup>
import { reactive, toRaw, watch } from 'vue';

    const formData = reactive({});
    const props= defineProps({
        initFormData: Object,
        formType: {
            type: String,
            required: true,
            validator: (value) => ['add', 'edit'].includes(value)
        }
    });
    const emit = defineEmits(['form-submit'])

    const submitClick = () => {
        emit('form-submit', toRaw(formData))

    }

    watch(
    () => props.initFormData,
    (newFormData) => {
        Object.assign(formData, newFormData ?? {});
    },
    {
        immediate: true, deep: true // watch가 등록될 때 즉시 한 번 실행한다.
    }

    );
</script>

<style lang="scss" scoped>

</style>