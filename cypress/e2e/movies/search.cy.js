import {MOVIE_SEARCH} from '../../constants'

describe('Movie search works correctly', () => {
    beforeEach(() => {
        // Visit movies page
        cy.visit('/media/movies')
    })

    it('Sucessfully searches and displays movie', () => {
        cy.get('main')
            .find('input[type="search"]')
            .should("exist")
            .should("have.attr", "placeholder", "Search")
            .type(MOVIE_SEARCH.searchTerm)
            .type('{enter}')
        
        cy.get('.card').first()
            .should('exist')
            .find('img')
            .should('have.attr', 'src', MOVIE_SEARCH.imageSrc)
        
        cy.get('.card').first()
            .should("exist")
            .find("h2")
            .should("exist")
            .should("not.be.empty")
            .contains(MOVIE_SEARCH.movieTitle)
    });
})