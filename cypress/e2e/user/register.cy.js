import {USER} from '../../constants'

describe('Register works correctly', () => {
    beforeEach(() => {
        cy.visit('/register')
    })

    it('Registering a user works', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false)

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

    it('Registering with invalid email format does not work', () => {
        let user = {
            email: `${USER.email}12345`,
            username: USER.username,
            displayName: USER.displayName,
            password: USER.password
        }

        cy.registerUser(user, false, false)
        
        cy.get('main')
            .get('.alert')
            .should("exist")
            .should("not.be.empty")
    });

    it('Registering with the same username should not work', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false)
            cy.registerUser(user, false, false)

            cy.get('main')
                .get('.alert')
                .should("exist")
                .should("not.be.empty")
                .contains(user.username)
        })
    });
})