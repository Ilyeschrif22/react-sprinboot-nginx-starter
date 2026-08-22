import { useState } from "react";
import "./App.css";

function App() {
  const VITE_BACKEND_URL = import.meta.env.VITE_BACKEND_URL;
  const [name, setName] = useState("");
  const [price, setPrice] = useState(0);
  const [products ,setProducts] = useState([]);

  const fetchProducts = async () => {
    console.log(VITE_BACKEND_URL)
    const response = await fetch(`${VITE_BACKEND_URL}/products`);
    const data = await response.json();
    setProducts(data);
  };
  fetchProducts();

  const handleSubmit = async (e) => {
    e.preventDefault();

    const response = await fetch(`${VITE_BACKEND_URL}/products`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        name: name,
        price: Number(price),
      }),
    });

    const data = await response.json();

    console.log(data);

    setName("");
    setPrice(0);
  };

  return (
    <div className="main">
      <input
        type="text"
        value={name}
        onChange={(e) => {
          setName(e.target.value);
        }}
      ></input>

      <input
        type="number"
        value={price}
        onChange={(e) => {
          setPrice(e.target.value);
        }}
      ></input>

      <button onClick={handleSubmit}>add product</button>

      <p>Liste produits</p>
      {products.map((product) =>(
        <div key={product.id}>
        <h3>{product.name}</h3>
        <p>Price: {product.price}</p>
        </div>
      ))}

    </div>

  );
}

export default App;
