describe('/media/books displays correctly', () => {
    beforeEach(() => {
        // Visit books
        cy.visit('/media/books')
    })

    it('Correctly navigates to books', () => {
        // Visit homepage
        cy.visit('/')

        // Find and search books button
        cy.get('.navbar')
            .find('a[href="/media/books"]')
            .should('exist')
            .click()
    })

    it('At least one books card exists and has correct image', () => {
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src')
            .and("not.be.empty")
    })

    it('Book name is displaying', () => {
        cy.get('.card').first()
            .should('exist')
            .find('h2')
            .should('exist')
            .invoke('text')
            .should('not.be.empty')
    })
})