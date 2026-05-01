<template>

    <RouterView class="container layout"></RouterView>

</template>

<script setup>

</script>

<style>
    html, body, #app {
        /* 각각의 요소가 부모 요소의 전체 높이를 채우도록 설정이다. */
        /* html → body → #app으로 이어지는 DOM 구조 전체가 브라우저 화면 전체를 덮도록 만든다. */
        height: 100%;
        /*  브라우저가 기본으로 주는 body의 여백(margin)을 제거해서 딱 맞게 붙는 레이아웃을 만든다. */
        margin: 0;
    }
    
    .layout {
        /* 요소 안의 자식들을 Flexbox로 정렬하겠다는 의미이다. */
        display: flex;
        /* 자식 요소들을 위에서 아래 방향(수직 방향)으로 쌓아준다. */
        /* 이 구조에서는 Header, RouterView, Footer가 자식으로 위에서 아래로 쌓인다. */
        flex-direction: column;
        /* layout 요소가 브라우저 화면 전체 높이(100% Viewport Height)를 최소 높이로 가지도록 설정한다. */
        /* 즉, 화면이 작든 크든 항상 Footer가 아래쪽에 있게 해주는 중요한 역할을 한다. */
        min-height: 100vh;
    }

    .content {
        /* Flexbox 자식 요소의 성장/축소/기본 크기 설정 */
        /* 1: 남은 공간을 가능한 많이 차지하라는 뜻이다. (이게 메인 콘텐츠 역할) */
        /* 0: 줄어들 수 없음 (화면이 작아져도 무조건 일정 이상 크기를 유지) */
        /* auto: 기본 크기는 콘텐츠의 높이만큼 */
        /* 즉, 남은 공간을 차지하고, 줄어들 수는 없음 */
        flex: 1 0 auto; 
        /* 내부에 여백을 줘서 콘텐츠가 좌우/상하로 너무 붙지 않게 한다. */
        padding: 1rem;
        /* 필요하면 스크롤도 가능 */
        /* 내용이 많을 경우 세로 스크롤이 자동으로 생기게 한다. */
        overflow-y: auto; 
    }
</style>

