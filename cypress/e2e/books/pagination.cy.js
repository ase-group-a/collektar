describe('Books pagination works correctly', () => {
    beforeEach(() => {
        // Visit books page
        cy.visit('/media/books')
    })

    it('Pagination updates media item cards', () => {
        cy.paginationTest()
    });
})