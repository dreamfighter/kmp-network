export let maxUsers = 10;

export class User {
    constructor (username) {
        this.username = username;
    }
}

function greet (name) {
    console.log("Hello, " + name + "!");
}

function toCurrency(amount) {
    return "" + amount;
}