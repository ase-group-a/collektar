describe('Movies pagination works correctly', () => {
    beforeEach(() => {
        // Visit movies page
        cy.visit('/media/movies')
    })

    it('Pagination updates media item cards', () => {
        cy.paginationTest()
    });
})