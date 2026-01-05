import {USER} from '../../constants'

describe('Collections work as expected', () => {
    
    it('Default collections should exist', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false)
            
            cy.get('main')
                .find("h2")
                .contains('Collections')
                .should('exist')

            cy.get('main')
                .find("h3")
                .contains('boardgames')
                .should('exist')

            cy.get('main')
                .find("h3")
                .contains('books')
                .should('exist')

            cy.get('main')
                .find("h3")
                .contains('games')
                .should('exist')

            cy.get('main')
                .find("h3")
                .contains('movies')
                .should('exist')

            cy.get('main')
                .find("h3")
                .contains('music')
                .should('exist')

            cy.get('main')
                .find("h3")
                .contains('shows')
                .should('exist')
        })
    });

    it('Collections should be able to be created and deleted', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false)

            cy.get('main')
                .find("button")
                .contains('+ Add Collection')
                .should('exist')
                .click()

            cy.get('main')
                .find('input[type="text"]')
                .should("exist")
                .type('Test Collection')

            cy.get('main')
                .find('button')
                .contains('Save')
                .should("exist")
                .click()

            cy.get('main')
                .find("h3")
                .contains('test collection')
                .should('exist')

            cy.get('main')
                .find('button')
                .contains('Edit')
                .should("exist")
                .click()

            cy.get('main')
                .find('button')
                .contains('Delete')
                .should("exist")
                .click()

            cy.get('main')
                .find("h3")
                .should("not.contain.text", 'test collection')

            cy.get('main')
                .find('button')
                .contains('Delete')
                .should("not.exist")
        })
    });
})