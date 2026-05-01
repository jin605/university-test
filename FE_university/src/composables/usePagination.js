import { computed, ref } from "vue";

/*
    Composable 함수
        - Composable 함수는 호출마다 독립 인스턴스(state)를 만들 수 있어 각 컴포넌트나 스토어에서 자유롭게 사용이 가능하다. 
*/
export const usePagination = () => {
    // 현재 페이지
    const currentPage = ref(1);
    // 전체 데이터 수
    const totalCount = ref(0);
    // 페이지네이션에 보이는 페이지 수
    const pageLimit = ref(5);
    // 한 페이지에 표시될 리스트의 수
    const listLimit = ref(10);

    /*
    전체 페이지의 수

    totalCount = 100, listLimit = 10
    100 / 10 = 10.0		=> 10페이지
    101 / 10 = 10.1		=> 11페이지
    103 / 10 = 10.3		=> 11페이지
    109 / 10 = 10.9		=> 11페이지
    110 / 10 = 11.0		=> 11페이지
    111 / 10 = 11.1		=> 12페이지
    */
    const totalPages = computed(() => Math.ceil(totalCount.value / listLimit.value));

    /*
    페이징 된 페이지 중 시작 페이지	
    < 1 2 3 4 5 6 7 8 9 10 >
    < 11 12 13 14 15 16 17 18 19 20 >
    < 21 22 23 24 25 26 27 28 29 30 >
    
    1, 11, 21, 31, .... => (10 * n) + 1 (n >= 0)
    
    1 ~ 10 : n = 0
    11 ~ 20 : n = 1
    21 ~ 30 : n = 2
    31 ~ 40 : n = 3 
    .... 
    n = (currentPage - 1) / pageLimit
    
    (10 * ((currentPage - 1) / pageLimit)) + 1 (n >= 0)
    */
    const startPage = computed(() => (pageLimit.value * Math.floor((currentPage.value - 1) / pageLimit.value)) + 1);

    // 페이징 된 페이지 중 마지막 페이지
    // 10, 20, 30, 40, .... 
    const endPage = computed(() => {
        const endPage = startPage.value + pageLimit.value - 1;

        return endPage > totalPages.value ? totalPages.value : endPage;
    });

    // 페이지 리스트 생성(반응형)
    const pageNumbers = computed(() => {
        const numbers = [];

        for (let i = startPage.value; i <= endPage.value; i++) {
            numbers.push(i);
        }

        return numbers;
    });

    const setCurrentPage = (page) => {
        currentPage.value = page;
    };

    const setTotalCount = (count) => {
        totalCount.value = count;
    }

    return { currentPage, listLimit, totalPages, pageNumbers, setCurrentPage, setTotalCount };
}