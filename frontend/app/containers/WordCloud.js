import React from 'react';

class Counter extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      counter: 0,
    };

    this.handleClick = this.handleClick.bind(this);
    this.handleSocketClose = this.handleSocketClose.bind(this);
    this.handleSocketMessage = this.handleSocketMessage.bind(this);
    this.handleSocketOpen = this.handleSocketOpen.bind(this);
  }

  componentDidMount() {
    this.socket = new WebSocket('ws://localhost:8083');
    this.socket.onopen = this.handleSocketOpen;
    this.socket.onclose = this.handleSocketClose;
    this.socket.onmessage = this.handleSocketMessage;
  }

  handleClick() {
    const { counter } = this.state;
    this.setState({
      counter: counter + 1,
    });
  }

  handleSocketClose() {
    console.log('socket close');
  }

  handleSocketMessage(message) {
    const wordCounts = JSON.parse(message.data);
    console.log('socket message:', wordCounts);
  }

  handleSocketOpen() {
    console.log('socket open');
  }
  
  render() {
    const { className, counter } = this.state;
    return (
      <div className={className}>
        <button
          onClick={this.handleClick}
          type="button"
        >
          Click me!
        </button>
        <div>
          {counter}
        </div>
      </div>
    );
  }
}

export default Counter;
