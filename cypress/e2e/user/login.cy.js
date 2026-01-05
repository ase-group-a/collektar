import {USER} from '../../constants'

describe('Login works correctly', () => {
    beforeEach(() => {
        cy.visit('/login')
    })
    
    it('Login with registered user should work', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, true, true) // Only register user but then logout
            
            cy.get('main')
                .find('input[placeholder="Username"]')
                .should('exist')
                .type(user.username)

            cy.get('main')
                .find('input[placeholder="Password"]')
                .should('exist')
                .type(user.password)

            cy.get('main')
                .find('button[type="submit"]')
                .contains('Login')
                .should('exist')
                .click()

            cy.get('.navbar')
                .get('.dropdown')
                .should("exist")
                .find('span')
                .contains(user.displayName)

            cy.get('main')
                .find("h1")
                .contains(user.displayName)

            cy.get('main')
                .find("p")
                .contains(user.username)
        })
    });

    it('Login with invalid user should not work', () => {
        cy.getRandomUser(USER).then(user => {

            cy.get('main')
                .find('input[placeholder="Username"]')
                .type(user.username)

            cy.get('main')
                .find('input[placeholder="Password"]')
                .type(user.password)

            cy.get('main')
                .find('button[type="submit"]')
                .contains('Login')
                .click()

            cy.get('main')
                .get('.alert')
                .should("exist")
                .should("not.be.empty")
        })
    });
})