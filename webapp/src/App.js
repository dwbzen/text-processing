import React, { Component } from 'react';
import logo from './logo.svg';
import './App.css';

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {date: new Date()};
    this.handleDrugsClick = this.handleDrugsClick.bind(this);
    this.handleInsultsClick = this.handleInsultsClick.bind(this);
  }

  drugsUrl = 'http://localhost:8080/text-processing/rest/TextService/drugs/20';
  insultsUrl = 'http://localhost:8080/text-processing/rest/TextService/insults/20';
  patternUrl = "http://localhost:8080/text-processing/rest/TextService/pattern/";  // /{number}

  handleDrugsClick() {

  }
  
  handleInsultsClick() {

  }

  handlePatternsClick() {

  }

  render() {
    return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          <h1 className="App-title">Welcome to React</h1>
        </header>
        <p className="App-intro">
          To get started, edit <code>src/App.js</code> and save to reload.
        </p>
        <ul>
          <li>
            <button onClick={this.handleDrugsClick}>Drugs</button>
          </li>
          <li>
            <button onClick={this.handleInsultsClick}>Insults</button>
          </li>
        </ul>
      <h2>Generate from Pattern</h2>
      <Bands/>
      </div>
    );
  }
}

class Bands extends Component {
  bandUrl = 'http://localhost:8080/text-processing/rest/TextService/bands/10';
  constructor(props) {
    super(props);
    this.handleClick = this.handleClick.bind(this);
  }

  handleClick() {
    console.log("clicked");
  }

  
  render() {
    return (
      <button type="submit" formAction={this.bandUrl} onClick={this.handleClick}>
        Generate
      </button>
    );
  }
}

export default App;
