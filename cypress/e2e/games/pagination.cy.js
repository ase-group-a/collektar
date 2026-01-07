describe('Game pagination works correctly', () => {
    beforeEach(() => {
        // Visit games page
        cy.visit('/media/games')
    })

    it('Pagination updates media item cards', () => {
        cy.paginationTest()
    });
})