describe('/media/books displays correctly', () => {

    it('should correctly navigate to books and display at least one media item', () => {
        // Visit homepage
        cy.visit('/')

        // Find and search books button
        cy.get('a[href="/media/books"]')
            .should('exist')
            .click()

        // At least one card exists
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src')
            .and("not.be.empty")

        cy.get('.card').first()
            .should('exist')
            .find('h2')
            .should('exist')
            .invoke('text')
            .should('not.be.empty')
    })
})