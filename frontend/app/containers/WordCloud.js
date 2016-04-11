import classNames from 'classnames';
import Immutable from 'immutable';
import React from 'react';
import styles from './WordCloud.scss';

class Counter extends React.Component {
  static propTypes = {
    className: React.PropTypes.string,
  };

  constructor(props) {
    super(props);

    this.state = {
      wordCounts: Immutable.Map(),
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
    const wordCounts = Immutable.fromJS(JSON.parse(message.data));
    console.log('socket message:', wordCounts.toJS());
    this.setState({
      wordCounts,
    });
  }

  handleSocketOpen() {
    console.log('socket open');
  }
  
  render() {
    const { className } = this.props;
    const { wordCounts } = this.state;

    const totalCount = wordCounts.reduce((result, count) => result + count, 0);

    return (
      <div className={classNames(styles.wordCloud, className)}>
        {wordCounts.sortBy(count => -count).entrySeq().map(([word, count]) => {
          return (
            <div
              className={styles.word}
              style={{
                fontSize: 12 + 100 * (count / totalCount),
              }}
            >
              {word}
            </div>
          );
        })}
      </div>
    );
  }
}

export default Counter;
