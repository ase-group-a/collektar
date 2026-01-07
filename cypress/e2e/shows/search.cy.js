import {SHOW_SEARCH} from '../../constants'

describe('Show search works correctly', () => {
    beforeEach(() => {
        // Visit shows page
        cy.visit('/media/shows')
    })

    it('Sucessfully searches and displays shows', () => {
        cy.get('main')
            .find('input[type="search"]')
            .should("exist")
            .should("have.attr", "placeholder", "Search")
            .type(SHOW_SEARCH.searchTerm)
            .type('{enter}')
        
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src', SHOW_SEARCH.imageSrc)
        
        cy.get('.card').first()
            .should("exist")
            .find("h2")
            .should("exist")
            .should("not.be.empty")
            .contains(SHOW_SEARCH.showTitle)
    });
})