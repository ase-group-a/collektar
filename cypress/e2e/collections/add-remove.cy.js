import {USER} from '../../constants'

describe('Collections work as expected', () => {

    it('Adding and removing game items to collections works', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false).then(() => {
                cy.collectionItemTest('games')
            })
        })
    })

    it('Adding and removing movie items to collections works', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false).then(() => {
                cy.collectionItemTest('movies')
            })
        })
    })

    it('Adding and removing show items to collections works', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false).then(() => {
                cy.collectionItemTest('shows')
            })
        })
    })

    it('Adding and removing music items to collections works', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false).then(() => {
                cy.collectionItemTest('music')
            })
        })
    })

    it('Adding and removing book items to collections works', () => {
        cy.getRandomUser(USER).then(user => {
            cy.registerUser(user, false, false).then(() => {
                cy.collectionItemTest('books')
            })
        })
    })
})