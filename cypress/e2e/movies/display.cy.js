describe('/media/movies displays correctly', () => {
    beforeEach(() => {
        // Visit movies
        cy.visit('/media/movies')
    })

    it('Correctly navigates to movies', () => {
        // Visit homepage
        cy.visit('/')

        // Find and search movies button
        cy.get('.navbar')
            .find('a[href="/media/movies"]')
            .should('exist')
            .click()
    })

    it('At least one movie card exists and has correct image', () => {
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src')
            .and("not.be.empty")
    })

    it('Movie name is displaying', () => {
        cy.get('.card').first()
            .should('exist')
            .find('h2')
            .should('exist')
            .invoke('text')
            .should('not.be.empty')
    })
})