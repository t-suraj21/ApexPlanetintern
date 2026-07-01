const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const path = require('path');
const fs = require('fs');
const multer = require('multer');
const { User, Cart, Order, Food } = require('./models');

const app = express();
const PORT = 5001;

app.use(cors());
app.use(express.json());

// Create upload directory if it doesn't exist
const uploadDir = path.join(__dirname, 'public', 'uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

// Serve uploaded profile images publicly
app.use('/uploads', express.static(uploadDir));

// Connect to MongoDB
mongoose.connect('mongodb://127.0.0.1:27017/foodiego')
  .then(() => {
    console.log('Connected to MongoDB');
    seedFoods();
  })
  .catch(err => console.error('MongoDB connection error:', err));

// Multer Storage Configuration for Profile Pictures
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
    cb(null, 'avatar-' + uniqueSuffix + path.extname(file.originalname));
  }
});
const upload = multer({ storage });

// --- Helper: Generate Unique IDs ---
function generateId() {
  return Math.random().toString(36).substring(2, 11);
}

// --- Seeding Default Foods Catalog ---
async function seedFoods() {
  const count = await Food.countDocuments();
  if (count === 0) {
    const defaultFoods = [
      {
        id: "1",
        name: "Premium Pepperoni Pizza",
        description: "Freshly baked pizza loaded with premium pepperoni, mozzarella cheese, and our signature rich tomato sauce.",
        price: "₹349",
        imageUrl: "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=500&auto=format&fit=crop&q=60",
        rating: "4.7",
        deliveryTime: "25 min"
      },
      {
        id: "2",
        name: "Classic Cheese Burger",
        description: "Flame-grilled juicy beef patty with fresh lettuce, tomatoes, melted cheddar cheese, and special sauce.",
        price: "₹189",
        imageUrl: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop&q=60",
        rating: "4.5",
        deliveryTime: "20 min"
      },
      {
        id: "3",
        name: "Creamy Alfredo Pasta",
        description: "Fettuccine tossed in a rich, creamy parmesan cheese and fresh cream sauce, topped with garlic herbs.",
        price: "₹249",
        imageUrl: "https://images.unsplash.com/photo-1645112411341-6c4fd023714a?w=500&auto=format&fit=crop&q=60",
        rating: "4.3",
        deliveryTime: "30 min"
      },
      {
        id: "4",
        name: "Club Sandwich",
        description: "Double-decker sandwich with toasted bread, grilled chicken, egg, crispy lettuce, tomato, and creamy mayo.",
        price: "₹149",
        imageUrl: "https://images.unsplash.com/photo-1521390188846-e2a3a97453a0?w=500&auto=format&fit=crop&q=60",
        rating: "4.2",
        deliveryTime: "15 min"
      },
      {
        id: "5",
        name: "Cold Brew Coffee",
        description: "Smooth, bold cold-brewed coffee served over ice blocks, sweetened lightly with milk of choice.",
        price: "₹99",
        imageUrl: "https://images.unsplash.com/photo-1517701604599-bb29b565090c?w=500&auto=format&fit=crop&q=60",
        rating: "4.6",
        deliveryTime: "10 min"
      },
      {
        id: "6",
        name: "Choco Fudge Sundae",
        description: "Creamy vanilla ice cream scoops drowned in hot chocolate fudge syrup and topped with crushed nuts.",
        price: "₹129",
        imageUrl: "https://images.unsplash.com/photo-1563805042-7684c019e1cb?w=500&auto=format&fit=crop&q=60",
        rating: "4.8",
        deliveryTime: "12 min"
      }
    ];
    await Food.insertMany(defaultFoods);
    console.log('Seeded default catalog foods into MongoDB');
  }
}

// --- API ROUTES ---

// 1. Get Foods Catalog
app.get('/api/products', async (req, res) => {
  try {
    const products = await Food.find({});
    res.json(products);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 2. Get Food by ID
app.get('/api/products/:id', async (req, res) => {
  try {
    const product = await Food.findOne({ id: req.params.id });
    if (!product) return res.status(404).json({ message: 'Product not found' });
    res.json(product);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 3. User Register
app.post('/api/auth/register', async (req, res) => {
  const { name, email, password } = req.body;
  try {
    const exists = await User.findOne({ email });
    if (exists) return res.status(400).json({ message: 'User already exists!' });

    const userId = generateId();
    const newUser = new User({ userId, name, email, password });
    await newUser.save();
    
    res.status(201).json(newUser);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 4. User Login
app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body;
  try {
    const user = await User.findOne({ email });
    if (!user || user.password !== password) {
      return res.status(400).json({ message: 'Invalid email or password!' });
    }
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 5. Get User Profile
app.get('/api/users/:userId', async (req, res) => {
  try {
    const user = await User.findOne({ userId: req.params.userId });
    if (!user) return res.status(404).json({ message: 'User not found' });
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 6. Update User Profile Name
app.put('/api/users/:userId', async (req, res) => {
  const { name } = req.body;
  try {
    const user = await User.findOneAndUpdate(
      { userId: req.params.userId },
      { name },
      { new: true }
    );
    if (!user) return res.status(404).json({ message: 'User not found' });
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 7. Upload User Profile Image
app.post('/api/users/:userId/avatar', upload.single('image'), async (req, res) => {
  try {
    if (!req.file) return res.status(400).json({ message: 'No file uploaded' });
    
    // Construct local public accessible server URL
    const host = req.get('host');
    const avatarUrl = `${req.protocol}://${host}/uploads/${req.file.filename}`;
    
    const user = await User.findOneAndUpdate(
      { userId: req.params.userId },
      { profileImage: avatarUrl },
      { new: true }
    );
    if (!user) return res.status(404).json({ message: 'User not found' });
    
    res.json(user);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 8. Get User Cart Items
app.get('/api/cart/:userId', async (req, res) => {
  try {
    const cart = await Cart.findOne({ userId: req.params.userId });
    res.json(cart ? cart.items : []);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 9. Sync/Overwrite User Cart Items
app.post('/api/cart/:userId', async (req, res) => {
  const { items } = req.body;
  try {
    const cart = await Cart.findOneAndUpdate(
      { userId: req.params.userId },
      { items },
      { new: true, upsert: true }
    );
    res.json({ status: 'success', message: 'Cart synced successfully', cart });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 10. Place Checkout Order
app.post('/api/orders', async (req, res) => {
  const { userId, items, totalPrice, timestamp } = req.body;
  try {
    const orderId = generateId();
    const newOrder = new Order({
      orderId,
      userId,
      items,
      totalPrice,
      timestamp: timestamp || Date.now(),
      status: 'Pending'
    });
    await newOrder.save();
    
    // Clear user cart upon successful order placement
    await Cart.findOneAndUpdate({ userId }, { items: [] });
    
    // Return order ID wrapped in a JSON object for standard client parsing
    res.status(201).json({ orderId });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// 11. Get User Order History
app.get('/api/orders/:userId', async (req, res) => {
  try {
    const orders = await Order.find({ userId: req.params.userId }).sort({ timestamp: -1 });
    res.json(orders);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
