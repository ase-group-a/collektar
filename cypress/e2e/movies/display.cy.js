describe('/media/movies displays correctly', () => {

    it('should correctly navigate to movies and display at least one media item', () => {
        // Visit homepage
        cy.visit('/')

        // Find and search movies button
        cy.get('a[href="/media/movies"]')
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