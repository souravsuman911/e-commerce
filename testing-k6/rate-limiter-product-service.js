import http from "k6/http";
import { sleep, check } from "k6";

export let options = {
    vus: 20,          // 20 concurrent users
    duration: "20s",  // run for 20 seconds
};

const TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbnVzZXIiLCJ1c2VySWQiOjEsInJvbGVzIjpbIlJPTEVfQURNSU4iXSwiaWF0IjoxNzYzODIyNzU2LCJleHAiOjE3NjM5MDkxNTZ9.Ao3hWq7-SlLD58PCqPylonWQpc3sKLvmTE21Xx3GDL8";

export default function () {
    let res = http.get("http://localhost:8086/api/products", {
        headers: {
            "Authorization": `Bearer ${TOKEN}`,
            "Content-Type": "application/json"
        }
    });

    check(res, {
        "is status 200 or 429": (r) => r.status === 200 || r.status === 429,
    });

    sleep(1); // give 1 second between hits
}
