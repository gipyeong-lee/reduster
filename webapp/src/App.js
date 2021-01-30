import './App.css';
import {memo, useEffect, useState} from "react";
import {Container, Form, Grid} from "semantic-ui-react";
import axios from "axios";
import NodeChart from "./components/NodeChart";

function App() {
    const [state, setState] = useState({
        buckets: [], items: [], counter: {}, isInit: false
    })

    const [server, setServer] = useState({host: '127.0.0.1', port: '6379'})
    const [data, setData] = useState({})
    const {buckets, items, counter, isInit} = state

    useEffect(() => {
        if (isInit === false)
            loadBuckets(state, setState)
    }, [isInit])

    function handleAddServer() {
        addServer(server, state, setState)
    }

    function handleRemoveServer() {
        removeServer(server, state, setState)
    }

    function inputChange(event) {
        setServer({...server, ...{[event.target.name]: event.target.value}})
    }

    function inputChangeData(event) {
        setData({...data, ...{[event.target.name]: event.target.value}})
    }

    function handleAddData() {
        addData(data, state, setState)
    }

    return (
        <Container textAlign={'center'}>
            <Grid centered>
                <Grid.Row columns={12}>
                    <Grid.Column width={12}>
                        <Form>
                            <Form.Group>
                                <Form.Input
                                    onChange={(event) => {
                                        inputChange(event)
                                    }}
                                    name={'host'}
                                    placeholder='127.0.0.1'
                                    defaultValue={server.host}
                                />
                                <Form.Input
                                    onChange={(event) => {
                                        inputChange(event)
                                    }}
                                    name={'port'}
                                    placeholder='6379'
                                    defaultValue={server.port}
                                />
                                <Form.Button content={'Add'} onClick={handleAddServer}/>
                                <Form.Button content={'remove'} color={'red'} onClick={handleRemoveServer}/>

                            </Form.Group>
                        </Form>
                    </Grid.Column>
                    <Grid.Column width={12}>
                        <Form>
                            <Form.Group>
                                <Form.Input
                                    onChange={(event) => {
                                        inputChangeData(event)
                                    }}
                                    name={'key'}
                                    placeholder='key'
                                    defaultValue={data.key}
                                />
                                <Form.Input
                                    onChange={(event) => {
                                        inputChangeData(event)
                                    }}
                                    name={'value'}
                                    placeholder='value'
                                    defaultValue={data.value}
                                />
                                <Form.Button content={'Add'} onClick={handleAddData}/>
                            </Form.Group>
                        </Form>
                    </Grid.Column>
                </Grid.Row>
                <Grid.Row columns={12}>
                    <Grid.Column width={12} textAlign={'center'}>
                        {buckets.length > 0 ? <NodeChart buckets={buckets} counter={counter}/>
                            : ''}
                    </Grid.Column>
                </Grid.Row>
            </Grid>
        </Container>
    );
}

async function loadBuckets(state, setState) {
    const bucketResponse = await axios.get("http://localhost:8080/api/v1/redis/server/info/all")
    const itemResponse = await axios.get("http://localhost:8080/api/v1/redis/keys/info/all")
    const counter = makeCounter(itemResponse.data.keys)
    setState({
        ...state, ...{
            buckets: bucketResponse.data.buckets.map((bucket) => {
                bucket.color = `#${Math.floor(bucket.info * 16777215).toString(16)}`
                return bucket
            }),
            items: itemResponse.data.keys,
            counter: counter,
            isInit: true
        }
    })
}

function makeCounter(items) {
    const nextCounter = {}
    items.forEach((item) => {
        if (!nextCounter[`${item.serverKey}`]) {
            nextCounter[`${item.serverKey}`] = 1
        } else {
            nextCounter[`${item.serverKey}`] += 1
        }
    })

    return nextCounter
}

function addData(data, state, setState) {
    axios.put(`http://localhost:8080/api/v1/redis/cmd/set/${encodeURI(data.key)}/${encodeURI(data.value)}`).then(() => {
        setState({...state, ...{isInit: false}})
    })
}

function addServer(server, state, setState) {
    axios.post("http://localhost:8080/api/v1/redis/server", server).then(() => {
        setState({...state, ...{isInit: false}})
    })
}

function removeServer(server, state, setState) {
    axios.delete("http://localhost:8080/api/v1/redis/server", {data: server}).then(() => {
        setState({...state, ...{isInit: false}})
    })
}

export default memo(App);
