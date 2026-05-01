<template>
    <nav aria-label="Page navigation example">
        <ul class="pagination justify-content-center">
            <li class="page-item">
                <a class="page-link" href="#" aria-label="Previous"
                    @click.prevent="changePage(currentPage - 1)">
                    <span aria-hidden="true">&laquo;</span>
                </a>
            </li>
            <li class="page-item" v-for="page in pageNumbers" :key="page">
                <a class="page-link" :class="{active: currentPage === page}" href="#"
                    @click.prevent="changePage(page)">
                    {{ page }}
                </a>
            </li>
            <li class="page-item">
                <a class="page-link" href="#" aria-label="Next"
                    @click.prevent="changePage(currentPage + 1)">
                    <span aria-hidden="true">&raquo;</span>
                </a>
            </li>
        </ul>
    </nav>
</template>

<script setup>
    const props = defineProps({
        currentPage: {
            type: Number,
            required: true
        },
        pageNumbers: {
            type: Array,
            required: true
        }
    });

    const emit = defineEmits(['change-page']);

    const changePage = (page) => {
        emit('change-page', page);
    };
</script>

<style scoped>

</style>