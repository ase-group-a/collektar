describe('Music pagination works correctly', () => {
    beforeEach(() => {
        // Visit music page
        cy.visit('/media/music')
    })

    it('Pagination updates media item cards', () => {
        cy.paginationTest()
    });
})