describe('Shows pagination works correctly', () => {
    beforeEach(() => {
        // Visit shows page
        cy.visit('/media/shows')
    })

    it('Pagination updates media item cards', () => {
        cy.paginationTest()
    });
})